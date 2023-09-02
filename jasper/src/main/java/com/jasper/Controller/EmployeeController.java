package com.jasper.Controller;

import com.jasper.Entity.Employee;
import com.jasper.Repository.EmployeeRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;

    public EmployeeController(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    //http://localhost:8080/api/employees/report/pdf
    @GetMapping("/report/pdf")
    public ResponseEntity<ByteArrayResource> generateReportPdf() throws JRException {
        List<Employee> employees = employeeRepository.findAll();

        // Load JasperReport template
        InputStream jasperStream = getClass().getResourceAsStream("/employee.jasper");
        
        if (jasperStream == null) {
            throw new RuntimeException("JasperReport template not found");
        }
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(jasperStream);

        // Compile the JasperReport
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, new JRBeanCollectionDataSource(employees));

        // Export to PDF
        byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

        // Create a Resource to serve the PDF
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=employee_report.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}
