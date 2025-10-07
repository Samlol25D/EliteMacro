package com.example.EliteMacro.elitemacro.controller;

import com.example.EliteMacro.elitemacro.model.Usuario;
import com.example.EliteMacro.elitemacro.repository.UsuarioRepository;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/reporte-usuarios-pdf")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generarReporteUsuarios() {
        try {
            System.out.println("=== GENERANDO REPORTE PDF DE USUARIOS ===");

            List<Usuario> usuarios = usuarioRepository.findAll();
            System.out.println("Usuarios encontrados para reporte: " + usuarios.size());

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, out);
            document.open();

            // Título
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLUE);
            Paragraph title = new Paragraph("REPORTE DE USUARIOS - ELITEMACRO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Fecha de generación
            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.GRAY);
            String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph fechaParagraph = new Paragraph("Generado el: " + fecha, dateFont);
            fechaParagraph.setAlignment(Element.ALIGN_RIGHT);
            fechaParagraph.setSpacingAfter(20);
            document.add(fechaParagraph);

            // Tabla de usuarios
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 3, 2});
            table.setSpacingBefore(20);

            // Encabezados de tabla
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            PdfPCell cell;

            cell = new PdfPCell(new Phrase("ID", headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("USUARIO", headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("ROL", headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase("ESTADO", headerFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);

            // Datos de usuarios
            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            for (Usuario usuario : usuarios) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(usuario.getId()), dataFont)));
                table.addCell(new PdfPCell(new Phrase(usuario.getUsername(), dataFont)));
                table.addCell(new PdfPCell(new Phrase(usuario.getRol(), dataFont)));

                PdfPCell estadoCell = new PdfPCell(new Phrase(usuario.isActivo() ? "ACTIVO" : "INACTIVO", dataFont));
                estadoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                estadoCell.setBackgroundColor(usuario.isActivo() ? new Color(200, 255, 200) : new Color(255, 200, 200));
                table.addCell(estadoCell);
            }

            document.add(table);

            // Resumen
            long activos = usuarios.stream().filter(Usuario::isActivo).count();
            long inactivos = usuarios.size() - activos;

            Paragraph resumen = new Paragraph("\n\nResumen: " + usuarios.size() + " usuarios totales - " +
                    activos + " activos - " + inactivos + " inactivos",
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            document.add(resumen);

            document.close();

            byte[] pdfBytes = out.toByteArray();
            System.out.println("Reporte PDF generado exitosamente. Tamaño: " + pdfBytes.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=reporte_usuarios_elitemacro.pdf");
            headers.setContentType(MediaType.APPLICATION_PDF);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            System.out.println("ERROR al generar reporte PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}