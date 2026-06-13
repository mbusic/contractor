package hr.qnr.contractor.service;

import hr.qnr.contractor.entity.Order;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public String generateQuote(Order o) {
        String today = LocalDate.now().format(DATE_FMT);
        return html("PONUDA", o, """
                <table class="info">
                  <tr><th>Broj naloga:</th><td>%s</td></tr>
                  <tr><th>Datum:</th><td>%s</td></tr>
                  <tr><th>Poslovnica:</th><td>%s</td></tr>
                  <tr><th>Klijent:</th><td>%s</td></tr>
                  <tr><th>Lokacija:</th><td>%s</td></tr>
                  <tr><th>Kontakt osoba:</th><td>%s</td></tr>
                  <tr><th>Telefon:</th><td>%s</td></tr>
                </table>
                <h3>Opis radova</h3>
                <p>%s</p>
                <h3>Procijenjeni troškovi</h3>
                %s
                <div class="signature-block">
                  <p>Datum: %s</p>
                  <p>Potpis ovlaštene osobe:</p>
                  <p class="sig-line">___________________________</p>
                </div>
                """.formatted(
                o.getOrderNumber(), today,
                branchName(o), clientName(o), str(o.getLocation()),
                str(o.getContactPerson()), str(o.getPhone()),
                str(o.getDescription()),
                costTable(
                        o.getEstimatedKm(), o.getEstimatedWorkHours(),
                        o.getEstimatedNumberOfWorkers(), o.getEstimatedTotalHours(),
                        o.getEstimatedMaterialCost()),
                today));
    }

    public String generateWorkOrder(Order o) {
        String photoHtml = o.getPhotos().isEmpty() ? "<p><em>Nema fotografija</em></p>" :
                o.getPhotos().stream()
                        .map(p -> "<img src=\"http://localhost:8080" + p.getUrl() + "\" class=\"thumb\">")
                        .reduce("", String::concat);

        return html("RADNI NALOG", o, """
                <table class="info">
                  <tr><th>Broj naloga:</th><td>%s</td></tr>
                  <tr><th>Poslovnica:</th><td>%s</td></tr>
                  <tr><th>Lokacija:</th><td>%s</td></tr>
                  <tr><th>Kontakt osoba:</th><td>%s</td></tr>
                  <tr><th>Telefon:</th><td>%s</td></tr>
                  <tr><th>Hitnost:</th><td>%s</td></tr>
                  <tr><th>Serviser:</th><td>%s</td></tr>
                </table>
                <h3>Opis radova</h3>
                <p>%s</p>
                <h3>Fotografije</h3>
                <div class="photos">%s</div>
                <h3>Bilješke na terenu</h3>
                <div class="blank-field"></div>
                <div class="blank-field"></div>
                <h3>Troškovi (za popunjavanje na terenu)</h3>
                <table class="field-table">
                  <tr><th>Radni sati</th><td></td></tr>
                  <tr><th>Broj radnika</th><td></td></tr>
                  <tr><th>Ukupno sati</th><td></td></tr>
                  <tr><th>Kilometri</th><td></td></tr>
                  <tr><th>Materijal (EUR)</th><td></td></tr>
                </table>
                <div class="signature-block">
                  <p>Potpis servisera:</p>
                  <p class="sig-line">___________________________</p>
                </div>
                """.formatted(
                o.getOrderNumber(), branchName(o), str(o.getLocation()),
                str(o.getContactPerson()), str(o.getPhone()), str(o.getUrgency()),
                o.getAssignedServicer() != null ? o.getAssignedServicer().getDisplayName() : "-",
                str(o.getDescription()), photoHtml));
    }

    public String generateReport(Order o) {
        String notesHtml = o.getNotes().isEmpty() ? "<p><em>Nema bilješki</em></p>" :
                o.getNotes().stream()
                        .map(n -> "<p><strong>" + n.getAuthorName() + ":</strong> " + n.getText() + "</p>")
                        .reduce("", String::concat);

        String photoHtml = o.getPhotos().isEmpty() ? "<p><em>Nema fotografija</em></p>" :
                o.getPhotos().stream()
                        .map(p -> "<img src=\"http://localhost:8080" + p.getUrl() + "\" class=\"thumb\">")
                        .reduce("", String::concat);

        return html("IZVJEŠTAJ O RADOVIMA", o, """
                <table class="info">
                  <tr><th>Broj naloga:</th><td>%s</td></tr>
                  <tr><th>Poslovnica:</th><td>%s</td></tr>
                  <tr><th>Klijent:</th><td>%s</td></tr>
                  <tr><th>Lokacija:</th><td>%s</td></tr>
                  <tr><th>Serviser:</th><td>%s</td></tr>
                  <tr><th>Status:</th><td>%s</td></tr>
                </table>
                <h3>Opis radova</h3>
                <p>%s</p>
                <h3>Bilješke</h3>
                %s
                <h3>Stvarni troškovi</h3>
                %s
                <h3>Fotografije</h3>
                <div class="photos">%s</div>
                <div class="signature-block">
                  <p>Potpis ovlaštene osobe:</p>
                  <p class="sig-line">___________________________</p>
                </div>
                """.formatted(
                o.getOrderNumber(), branchName(o), clientName(o), str(o.getLocation()),
                o.getAssignedServicer() != null ? o.getAssignedServicer().getDisplayName() : "-",
                o.getStatus().name(),
                str(o.getDescription()),
                notesHtml,
                costTable(o.getActualKm(), o.getActualWorkHours(),
                        o.getActualNumberOfWorkers(), o.getActualTotalHours(),
                        o.getActualMaterialCost()),
                photoHtml));
    }

    public String generateInvoice(Order o) {
        String today = LocalDate.now().format(DATE_FMT);
        String created = o.getCreatedAt() != null
                ? o.getCreatedAt().atZone(ZoneId.systemDefault()).format(DATE_FMT) : today;

        return html("RAČUN", o, """
                <table class="info">
                  <tr><th>Broj naloga:</th><td>%s</td></tr>
                  <tr><th>Datum naloga:</th><td>%s</td></tr>
                  <tr><th>Datum računa:</th><td>%s</td></tr>
                  <tr><th>Poslovnica:</th><td>%s</td></tr>
                  <tr><th>Klijent:</th><td>%s</td></tr>
                  <tr><th>Lokacija:</th><td>%s</td></tr>
                </table>
                <h3>Obračun troškova</h3>
                %s
                <p class="vat-placeholder">[ Ovdje ide prikaz PDV-a i zakonski obvezni podaci za račun ]</p>
                <div class="signature-block">
                  <p>Datum: %s</p>
                  <p>Potpis i pečat:</p>
                  <p class="sig-line">___________________________</p>
                </div>
                """.formatted(
                o.getOrderNumber(), created, today,
                branchName(o), clientName(o), str(o.getLocation()),
                costTable(o.getActualKm(), o.getActualWorkHours(),
                        o.getActualNumberOfWorkers(), o.getActualTotalHours(),
                        o.getActualMaterialCost()),
                today));
    }

    // --- helpers ---

    private String html(String title, Order o, String body) {
        return """
                <!DOCTYPE html>
                <html lang="hr">
                <head>
                  <meta charset="UTF-8">
                  <title>%s – %s</title>
                  <style>
                    body { font-family: Arial, sans-serif; font-size: 14px; margin: 40px; color: #222; }
                    h1 { color: #2e7d32; border-bottom: 2px solid #2e7d32; padding-bottom: 6px; }
                    h3 { margin-top: 24px; color: #333; }
                    table.info { border-collapse: collapse; width: 100%%; margin-bottom: 16px; }
                    table.info th { text-align: left; width: 160px; padding: 4px 8px; background: #f5f5f5; }
                    table.info td { padding: 4px 8px; }
                    table.cost, table.field-table { border-collapse: collapse; width: 100%%; }
                    table.cost th, table.cost td,
                    table.field-table th, table.field-table td {
                      border: 1px solid #ccc; padding: 6px 12px; }
                    table.cost th, table.field-table th { background: #e8f5e9; }
                    table.field-table td { width: 200px; height: 28px; }
                    .photos img.thumb { width: 150px; height: 110px; object-fit: cover;
                      margin: 4px; border: 1px solid #ccc; border-radius: 4px; }
                    .blank-field { border-bottom: 1px solid #999; height: 32px; margin: 8px 0; }
                    .signature-block { margin-top: 48px; }
                    .sig-line { margin-top: 40px; }
                    .vat-placeholder { border: 1px dashed #f57c00; padding: 12px; color: #e65100;
                      background: #fff8e1; border-radius: 4px; }
                    @media print { body { margin: 20px; } }
                  </style>
                </head>
                <body>
                  <h1>%s</h1>
                  %s
                </body>
                </html>
                """.formatted(title, o.getOrderNumber(), title, body);
    }

    private String costTable(Integer km, Double workH, Integer workers, Double totalH, BigDecimal material) {
        return """
                <table class="cost">
                  <tr><th>Stavka</th><th>Vrijednost</th></tr>
                  <tr><td>Kilometri</td><td>%s km</td></tr>
                  <tr><td>Radni sati</td><td>%s h</td></tr>
                  <tr><td>Broj radnika</td><td>%s</td></tr>
                  <tr><td>Ukupno sati</td><td>%s h</td></tr>
                  <tr><td>Materijal</td><td>%s EUR</td></tr>
                </table>
                """.formatted(
                km != null ? km : "-",
                workH != null ? workH : "-",
                workers != null ? workers : "-",
                totalH != null ? totalH : "-",
                material != null ? material : "-");
    }

    private String branchName(Order o) {
        return o.getBranch() != null ? o.getBranch().getName() : "-";
    }

    private String clientName(Order o) {
        return o.getClient() != null ? o.getClient().getName() : "-";
    }

    private String str(String s) {
        return s != null ? s : "-";
    }
}
