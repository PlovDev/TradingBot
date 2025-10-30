package com.plovdev.bot.bots;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.sql.*;
import java.util.List;

public class Exporter {
    private static final List<String> disableColumns = List.of("apiKey", "secretKey", "phrase", "");

    public static void export(String from, String to) {
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + from);
             Workbook workbook = new XSSFWorkbook()) {
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
                Sheet sheet = workbook.createSheet("Users");

                try (Statement statement = connection.createStatement();
                     ResultSet set = statement.executeQuery("SELECT * FROM Users")) {
                    ResultSetMetaData rsdata = set.getMetaData();
                    int clumns = rsdata.getColumnCount();

                    Row header = sheet.createRow(0);

                    for (int i = 1; i <= clumns; i++) {
                        String column = rsdata.getColumnName(i);
                        if (!disableColumns.contains(column)) {
                            Cell cell = header.createCell(i - 1);
                            if (column.equals("beerj")) column = "exchange";
                            cell.setCellValue(column);
                            cell.setCellStyle(style);
                        }
                    }

                    int rowNum = 1;
                    while (set.next()) {
                        Row row = sheet.createRow(rowNum++);
                        for (int i = 1; i <= clumns; i++) {
                            String column = rsdata.getColumnName(i);
                            if (!disableColumns.contains(column)) {
                                Cell cell = row.createCell(i - 1);
                                Object value = set.getObject(i);
                                if (value != null) {
                                    cell.setCellValue(value.toString());
                                }
                            }
                        }
                    }
                    for (int i = 0; i < clumns; i++) sheet.autoSizeColumn(i);
                }


            try (FileOutputStream stream = new FileOutputStream(to)) {
                workbook.write(stream);
                System.out.println("Done");
            } catch (Exception e) {
                System.out.println("Export error");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}