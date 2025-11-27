package controller;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.bson.Document; // Necessário para ler os dados do MongoDB
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class RelatorioOngs {

    public boolean gerar(List<Document> listaOngs, String filePath) {

        com.itextpdf.text.Document pdfDocument = new com.itextpdf.text.Document();

        try {
            PdfWriter.getInstance(pdfDocument, new FileOutputStream(filePath));
            pdfDocument.open();

            pdfDocument.add(new Paragraph("Relatório de ONGs Cadastradas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Font.NORMAL, BaseColor.BLACK)));
            pdfDocument.add(Chunk.NEWLINE);

            // Criação da Tabela (4 Colunas)
            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);

            // Cabeçalho da Tabela
            table.addCell(new Phrase("Nome Fantasia", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("CNPJ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Causa Social", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));
            table.addCell(new Phrase("Email", FontFactory.getFont(FontFactory.HELVETICA_BOLD)));

            // Preenche os Dados
            for (Document ong : listaOngs) {

                table.addCell(ong.getString("nomeFantasia"));
                table.addCell(ong.getString("cnpj"));
                table.addCell(ong.getString("causaSocial"));
                table.addCell(ong.getString("email"));
            }

            pdfDocument.add(table);
            pdfDocument.close();
            return true;
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
