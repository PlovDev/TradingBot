package com.plovdev.bot.modules.beerjes;

import com.plovdev.bot.bots.Button;
import com.plovdev.bot.bots.LanguageManager;
import com.plovdev.bot.main.TestUtils;
import com.plovdev.bot.modules.databases.BlanksDB;
import com.plovdev.bot.modules.databases.ReferralDB;
import com.plovdev.bot.modules.databases.UserDB;
import com.plovdev.bot.modules.databases.UserEntity;
import com.plovdev.bot.modules.messages.Pending;
import com.plovdev.bot.modules.models.OrderResult;
import com.plovdev.bot.modules.models.SymbolInfo;
import com.plovdev.bot.modules.parsers.Signal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SignalOpener {
    private static final Logger log = LoggerFactory.getLogger(SignalOpener.class);
    private final LanguageManager manager = new LanguageManager();
    private final UserDB userDB = new UserDB();
    private final ReferralDB referralDB = new ReferralDB();
    private final BlanksDB blanksDB = new BlanksDB();
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final TelegramLongPollingBot bot;

    public SignalOpener(TelegramLongPollingBot bot) {
        this.bot = bot;
    }

    public CompletableFuture<List<OrderResult>> openSignal(Signal signal) {
        // Загружаем общие данные
        CompletableFuture<SymbolInfo> getBI = CompletableFuture.supplyAsync(() -> TestUtils.bitgetService.getSymbolInfo(TestUtils.bitgetTestUser, signal.getSymbol()));
        CompletableFuture<BigDecimal> getBEP = CompletableFuture.supplyAsync(() -> TestUtils.bitgetService.getEntryPrice(signal.getSymbol()));
        CompletableFuture<SymbolInfo> getUI = CompletableFuture.supplyAsync(() -> TestUtils.bitunixService.getSymbolInfo(TestUtils.bitunixUser, signal.getSymbol()));
        CompletableFuture<BigDecimal> getUEP = CompletableFuture.supplyAsync(() -> TestUtils.bitunixService.getEntryPrice(signal.getSymbol()));

        return CompletableFuture.allOf(getBI, getUI, getBEP, getUEP)
                .thenCompose(v -> {
                    SymbolInfo bInfo;
                    SymbolInfo uInfo;
                    BigDecimal bep;
                    BigDecimal uep;

                    try {
                        bInfo = getBI.get();
                        uInfo = getUI.get();
                        bep = getBEP.get();
                        uep = getUEP.get();
                    } catch (Exception e) {
                        log.error("Failed to fetch symbol/price data", e);
                        return CompletableFuture.failedFuture(e); // пробрасываем ошибку
                    }

                    log.info("First data getted: binfo: {}, uinfo: {}, bep: {}, uep: {}", bInfo, uInfo, bep, uep);

                    List<UserEntity> activeUsers = userDB.getAll().stream()
                            .filter(u -> "ACTIVE".equals(u.getStatus()))
                            .toList();

                    log.info("🚀 Executing signal for {} active users", activeUsers.size());

                    List<CompletableFuture<OrderResult>> futures = activeUsers.stream()
                            .map(user -> {
                                log.info("Start process signal for user. Username: {}", user.getTgName());
                                return processUser(executor, user, signal, bInfo, uInfo, bep, uep);
                            })
                            .toList();

                    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                            .thenApply(v2 -> futures.stream().map(CompletableFuture::join).toList());
                })
                .exceptionally(e -> {
                    log.error("❌ Signal execution failed: {}", e.getMessage(), e);
                    return List.of();
                });
    }


    private CompletableFuture<OrderResult> processUser(ExecutorService executor, UserEntity repository, Signal signal, SymbolInfo bsi, SymbolInfo usi, BigDecimal bep, BigDecimal uep) {
        return CompletableFuture.supplyAsync(() -> {
            OrderResult result = OrderResult.no();
            try {
                TradeService service = repository.getUserBeerj();
                int all = service.getPositions(repository).size();
                String positions = repository.getPositions();
                if (positions.equals("all")) {
                    if (service instanceof BitGetTradeService) {
                        result = service.openOrder(signal, repository, bsi, bep);
                    } else {
                        result = service.openOrder(signal, repository, usi, uep);
                    }
                    System.out.println(result);
                    if (result.succes() && !repository.getReferral().equals("none")) {
                        System.out.println("managing");
                        manageReferral(repository);
                    }
                } else {
                    log.info("Open order for user");
                    if ((Integer.parseInt(repository.getPositions()) - all) > 0) {
                        if (service instanceof BitGetTradeService) {
                            log.info("Open bitget order");
                            result = service.openOrder(signal, repository, bsi, bep);
                        } else {
                            result = service.openOrder(signal, repository, usi, uep);
                        }
                        System.out.println(result);
                        if (result.succes() && !repository.getReferral().equals("none")) {
                            manageReferral(repository);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error process user signal: ", e);
            }
            return result;
        }, executor);
    }


    private void manageReferral(UserEntity repository) {
        int posOpened = repository.getPosOpened() + 1;
        repository.setPosOpened(posOpened);
        userDB.update("posOpened", String.valueOf(posOpened), repository.getTgId());
        if (posOpened >= 50) {
            repository.setActiveRef(true);
            userDB.update("isActiveRef", "true", repository.getTgId());

            UserEntity repo = (UserEntity) userDB.get(repository.getReferral());
            int actives = repo.getActiveRefCount() + 1;
            repo.setActiveRefCount(actives);
            userDB.update("activeRefCount", String.valueOf(actives), repo.getTgId());

            if (actives >= 10) {
                SendMessage message = new SendMessage(repository.getReferral(), manager.getText(repo.getLanguage(), "referrDone"));
                blanksDB.add(repo.getTgId(), repo.getUID(), repo.getTgName(), "waiting", "ref", repo.getBeerj());
                Button button = new Button(manager.getText(repo.getLanguage(), "pendReferr"), "REFERRAL_PENDING:" + repository.getTgId());
                button.setActionListener(((update, message1, from, chatId, text, repository1) -> {
                    EditMessageText edit = new EditMessageText(manager.getText(((UserEntity) repository1).getLanguage(), "refPending"));
                    edit.setMessageId(message1.getMessageId());
                    bot.execute(edit);

                    userDB.getAll().stream()
                            .filter(e -> e.getRole().equals("admin"))
                            .forEach(repos -> {
                                String header = "<b>Новая заявка на реферал!</b>\n\n";
                                String tgId = "ID в телеграм: <b>" + repo.getTgId() + "</b>\n";
                                String name = "Имя: " + repo.getTgName() + "\n";
                                String uid = "UID на beerже: <b>" + repo.getUID() + "</b>";
                                Pending pending = new Pending(repos.getTgId(), header + tgId + name + uid, repo.getTgId(), bot, "none", "referrAccept", "referrReject");
                                try {
                                    bot.execute(pending);
                                } catch (TelegramApiException e) {
                                    log.error("Ошибка отправки заявки: {}", e.getMessage());
                                }
                            });

                }));
                message.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(button))));
                try {
                    bot.execute(message);
                    userDB.update("activeRefCount", "0", repo.getTgId());
                    userDB.update("inited", "0", repo.getTgId());

                    userDB.update("isActiveRef", "false", repository.getTgId());
                    userDB.update("posOpened", "0", repository.getTgId());
                } catch (TelegramApiException e) {
                    log.error("Telegram ref error: ", e);
                }
            }
        }
        try {
            referralDB.upadateByKey("positions", "0", repository.getReferral());
            referralDB.upadateByKey("invited", "0", repository.getReferral());
        } catch (Exception e) {
            log.error("Произошла ошибка выпонения реферала: {}", e.getMessage());
        }
    }
}