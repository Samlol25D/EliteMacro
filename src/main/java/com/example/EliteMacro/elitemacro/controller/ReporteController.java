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
import java.util.List;

@RestController
@RequestMapping("/api/admin/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> generarReporteUsuarios() {
        try {
            List<Usuario> usuarios = usuarioRepository.findAll();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
            Paragraph title = new Paragraph("Reporte de Usuarios - EliteMacro", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{1, 3, 3, 2});

            table.addCell(new PdfPCell(new Phrase("ID")));
            table.addCell(new PdfPCell(new Phrase("Usuario")));
            table.addCell(new PdfPCell(new Phrase("Rol")));
            table.addCell(new PdfPCell(new Phrase("Activo")));

            for (Usuario u : usuarios) {
                table.addCell(String.valueOf(u.getId()));
                table.addCell(u.getUsername());
                table.addCell(u.getRol());
                table.addCell(u.isActivo() ? "Sí" : "No");
            }

            document.add(table);
            document.close();

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=usuarios_elitemacro.pdf");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(out.toByteArray());

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(null);
        }
    }
}