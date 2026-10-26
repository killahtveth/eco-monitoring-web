package ru.coursework.eco_monitoring_backend.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ru.coursework.eco_monitoring_backend.controller.ReportController.ReportRow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExportService {

    public byte[] toXlsx(List<ReportRow> rows) throws IOException {
        try (var wb = new XSSFWorkbook(); var out = new ByteArrayOutputStream()) {
            var sheet = wb.createSheet("Отчёт");
            var header = sheet.createRow(0);
            String[] cols = {"Показатель", "Ед. изм.", "Min", "Max", "Avg", "Превышений", "Записей"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int r = 1;
            for (var row : rows) {
                var x = sheet.createRow(r++);
                x.createCell(0).setCellValue(row.parameter());
                x.createCell(1).setCellValue(row.unit());
                x.createCell(2).setCellValue(row.min());
                x.createCell(3).setCellValue(row.max());
                x.createCell(4).setCellValue(row.avg());
                x.createCell(5).setCellValue(row.exceeded());
                x.createCell(6).setCellValue(row.count());
            }
            wb.write(out);
            return out.toByteArray();
        }
    }
}