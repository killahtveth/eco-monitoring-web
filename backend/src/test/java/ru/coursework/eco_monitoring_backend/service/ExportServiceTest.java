package ru.coursework.eco_monitoring_backend.service;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import ru.coursework.eco_monitoring_backend.controller.ReportController.ReportRow;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportServiceTest {

    private final ExportService service = new ExportService();

    @Test
    void producesValidXlsxWithHeadersAndRows() throws Exception {
        List<ReportRow> rows = List.of(
            new ReportRow("PM2.5", "μg/m³", 10.0, 30.0, 20.0, 2, 3),
            new ReportRow("CO2", "ppm", 400.0, 800.0, 600.0, 1, 2)
        );

        byte[] bytes = service.toXlsx(rows);
        assertThat(bytes).isNotEmpty();

        try (var wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            var sheet = wb.getSheet("Отчёт");
            assertThat(sheet).isNotNull();
            var header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("Показатель");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("Ед. изм.");

            var r1 = sheet.getRow(1);
            assertThat(r1.getCell(0).getStringCellValue()).isEqualTo("PM2.5");
            assertThat(r1.getCell(2).getNumericCellValue()).isEqualTo(10.0);
            assertThat(r1.getCell(5).getNumericCellValue()).isEqualTo(2);
        }
    }

    @Test
    void handlesEmptyRows() throws Exception {
        byte[] bytes = service.toXlsx(List.of());
        assertThat(bytes).isNotEmpty();
        try (var wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertThat(wb.getSheet("Отчёт").getLastRowNum()).isEqualTo(0);
        }
    }
}