package com.plovdev.bot.bots;

import com.plovdev.bot.modules.beerjes.Position;
import com.plovdev.bot.modules.models.PositionsModel;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;

public class ExportPositions {
    public static void exportHistory(String to, String sheetName, List<String> cols, List<PositionsModel> positions) {
        try (Workbook workbook = new XSSFWorkbook();
        FileOutputStream stream = new FileOutputStream(to)) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);


            Sheet sheet = workbook.createSheet(sheetName);

            try {
                int clumns = cols.size();

                Row header = sheet.createRow(0);

                for (int i = 0; i < clumns; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(cols.get(i));
                    cell.setCellStyle(style);
                }

                int rowNum = 1;
                for (PositionsModel model : positions) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(model.getPair());
                    row.createCell(1).setCellValue(model.getDirection());
                    row.createCell(2).setCellValue(model.getOpen());
                    row.createCell(3).setCellValue(model.getClose());
                    row.createCell(4).setCellValue(model.getTotal());
                }
                for (int i = 0; i < clumns; i++) sheet.autoSizeColumn(i);
                workbook.write(stream);
                System.out.println("Done");
            } catch (Exception e) {
                System.out.println("Export error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void exportCurrent(String to, String sheetName, List<String> cols, List<Position> positions) {
        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream stream = new FileOutputStream(to)) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);


            Sheet sheet = workbook.createSheet(sheetName);

            try {
                int clumns = cols.size();

                Row header = sheet.createRow(0);

                for (int i = 0; i < clumns; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(cols.get(i));
                    cell.setCellStyle(style);
                }

                int rowNum = 1;
                for (Position model : positions) {
                    Row row = sheet.createRow(rowNum++);

                    row.createCell(0).setCellValue(model.getSymbol());
                    row.createCell(1).setCellValue(model.getHoldSide());
                    row.createCell(2).setCellValue(model.getMarketPrice().toString());
                    row.createCell(3).setCellValue(String.valueOf(model.getLeverage()));
                    row.createCell(4).setCellValue(model.getUnrealizedPL().toString());
                }
                for (int i = 0; i < clumns; i++) sheet.autoSizeColumn(i);
                workbook.write(stream);
                System.out.println("Done");
            } catch (Exception e) {
                System.out.println("Export error");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}