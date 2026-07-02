package hr.qnr.contractor.service;

import hr.qnr.contractor.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class DocumentService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Value("${app.base-url}")
    private String baseUrl;

    // -------------------------------------------------------------------------
    // Public document generators
    // -------------------------------------------------------------------------

    public String generateQuote(Order o) {
        String today = today();
        String body = """
                <table class="info">
                  <tr><th>Broj naloga</th><td>%s</td></tr>
                  <tr><th>Datum</th><td>%s</td></tr>
                  <tr><th>Poslovnica</th><td>%s</td></tr>
                  <tr><th>Klijent</th><td>%s</td></tr>
                  <tr><th>Lokacija</th><td>%s</td></tr>
                  <tr><th>Kontakt osoba</th><td>%s</td></tr>
                  <tr><th>Telefon</th><td>%s</td></tr>
                  <tr><th>Hitnost</th><td>%s</td></tr>
                </table>

                <h3>Opis radova</h3>
                <p class="description">%s</p>

                <h3>Procijenjeni troškovi</h3>
                %s

                <div class="signature-block">
                  <div class="sig-row">
                    <div>
                      <p class="sig-label">Mjesto i datum:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                    <div>
                      <p class="sig-label">Potpis i pečat:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                o.getOrderNumber(), today, branch(o), client(o),
                s(o.getLocation()), s(o.getContactPerson()), s(o.getPhone()), s(o.getUrgency()),
                s(o.getDescription()),
                costTable(o.getEstimatedKm(), o.getEstimatedWorkHours(),
                        o.getEstimatedNumberOfWorkers(), o.getEstimatedTotalHours(),
                        o.getEstimatedMaterialCost()));
        return page("PONUDA", o.getOrderNumber(), body);
    }

    public String generateWorkOrder(Order o) {
        String photos = photoGrid(o);
        String body = """
                <table class="info">
                  <tr><th>Broj naloga</th><td>%s</td></tr>
                  <tr><th>Datum</th><td>%s</td></tr>
                  <tr><th>Poslovnica</th><td>%s</td></tr>
                  <tr><th>Lokacija</th><td>%s</td></tr>
                  <tr><th>Kontakt osoba</th><td>%s</td></tr>
                  <tr><th>Telefon</th><td>%s</td></tr>
                  <tr><th>Hitnost</th><td>%s</td></tr>
                  <tr><th>Dodijeljeni serviser</th><td>%s</td></tr>
                </table>

                <h3>Opis radova</h3>
                <p class="description">%s</p>

                <h3>Fotografije</h3>
                %s

                <h3>Bilješke na terenu</h3>
                <div class="field-box"></div>
                <div class="field-box"></div>

                <h3>Troškovi (popunjava serviser)</h3>
                <table class="cost">
                  <tr><th>Radni sati</th><td></td></tr>
                  <tr><th>Broj radnika</th><td></td></tr>
                  <tr><th>Ukupno sati</th><td></td></tr>
                  <tr><th>Kilometri</th><td></td></tr>
                  <tr><th>Materijal (EUR)</th><td></td></tr>
                </table>

                <div class="signature-block">
                  <div class="sig-row">
                    <div>
                      <p class="sig-label">Datum i potpis servisera:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                    <div>
                      <p class="sig-label">Potpis naručitelja:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                o.getOrderNumber(), today(), branch(o), s(o.getLocation()),
                s(o.getContactPerson()), s(o.getPhone()), s(o.getUrgency()),
                o.getAssignedServicer() != null ? o.getAssignedServicer().getDisplayName() : "-",
                s(o.getDescription()), photos);
        return page("RADNI NALOG", o.getOrderNumber(), body);
    }

    public String generateReport(Order o) {
        String notes = o.getNotes().isEmpty()
                ? "<p><em>Nema bilješki.</em></p>"
                : o.getNotes().stream()
                        .map(n -> "<div class=\"note\"><span class=\"note-author\">" + n.getAuthorName()
                                + "</span> &mdash; " + n.getText() + "</div>")
                        .reduce("", String::concat);
        String photos = photoGrid(o);

        String body = """
                <table class="info">
                  <tr><th>Broj naloga</th><td>%s</td></tr>
                  <tr><th>Datum</th><td>%s</td></tr>
                  <tr><th>Poslovnica</th><td>%s</td></tr>
                  <tr><th>Klijent</th><td>%s</td></tr>
                  <tr><th>Lokacija</th><td>%s</td></tr>
                  <tr><th>Serviser</th><td>%s</td></tr>
                  <tr><th>Status</th><td>%s</td></tr>
                </table>

                <h3>Opis radova</h3>
                <p class="description">%s</p>

                <h3>Bilješke</h3>
                %s

                <h3>Stvarni troškovi</h3>
                %s

                <h3>Fotografije</h3>
                %s

                <div class="signature-block">
                  <div class="sig-row">
                    <div>
                      <p class="sig-label">Mjesto i datum:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                    <div>
                      <p class="sig-label">Potpis ovlaštene osobe:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                o.getOrderNumber(), today(), branch(o), client(o), s(o.getLocation()),
                o.getAssignedServicer() != null ? o.getAssignedServicer().getDisplayName() : "-",
                statusLabel(o.getStatus()),
                s(o.getDescription()), notes,
                costTable(o.getActualKm(), o.getActualWorkHours(),
                        o.getActualNumberOfWorkers(), o.getActualTotalHours(),
                        o.getActualMaterialCost()),
                photos);
        return page("IZVJEŠTAJ O RADOVIMA", o.getOrderNumber(), body);
    }

    public String generateInvoice(Order o) {
        String created = o.getCreatedAt() != null
                ? o.getCreatedAt().atZone(ZoneId.systemDefault()).format(DATE_FMT) : today();

        String body = """
                <table class="info">
                  <tr><th>Broj naloga</th><td>%s</td></tr>
                  <tr><th>Datum naloga</th><td>%s</td></tr>
                  <tr><th>Datum računa</th><td>%s</td></tr>
                  <tr><th>Poslovnica</th><td>%s</td></tr>
                  <tr><th>Klijent</th><td>%s</td></tr>
                  <tr><th>Lokacija</th><td>%s</td></tr>
                </table>

                <h3>Obračun troškova</h3>
                %s

                <div class="vat-box">
                  ⚠ Ovdje se upisuju zakonski obvezni podaci o PDV-u, OIB-u i broju računa.
                  (Placeholder – nije implementirano u POC verziji.)
                </div>

                <div class="signature-block">
                  <div class="sig-row">
                    <div>
                      <p class="sig-label">Mjesto i datum:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                    <div>
                      <p class="sig-label">Potpis i pečat:</p>
                      <p class="sig-line">_______________________</p>
                    </div>
                  </div>
                </div>
                """.formatted(
                o.getOrderNumber(), created, today(), branch(o), client(o), s(o.getLocation()),
                costTable(o.getActualKm(), o.getActualWorkHours(),
                        o.getActualNumberOfWorkers(), o.getActualTotalHours(),
                        o.getActualMaterialCost()));
        return page("RAČUN", o.getOrderNumber(), body);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private String page(String title, String orderNumber, String body) {
        return """
                <!DOCTYPE html>
                <html lang="hr">
                <head>
                  <meta charset="UTF-8">
                  <title>%s – %s</title>
                  <style>
                    *, *::before, *::after { box-sizing: border-box; }
                    body {
                      font-family: Arial, Helvetica, sans-serif;
                      font-size: 13px; color: #222; margin: 0; padding: 32px 40px;
                    }
                    h1 {
                      color: #2e7d32; border-bottom: 2px solid #2e7d32;
                      padding-bottom: 6px; margin: 0 0 20px; font-size: 1.5rem;
                    }
                    h3 { margin: 20px 0 6px; font-size: .95rem; color: #444; }
                    p  { margin: 4px 0; }

                    table.info { border-collapse: collapse; width: 100%%; margin-bottom: 4px; }
                    table.info th {
                      text-align: left; width: 170px; padding: 5px 10px;
                      background: #f1f8e9; font-weight: 600; border: 1px solid #ddd;
                    }
                    table.info td { padding: 5px 10px; border: 1px solid #ddd; }

                    table.cost { border-collapse: collapse; width: 100%%; }
                    table.cost th, table.cost td {
                      border: 1px solid #ccc; padding: 6px 12px; font-size: .88rem;
                    }
                    table.cost th { background: #e8f5e9; text-align: left; width: 200px; }
                    table.cost td { text-align: right; }

                    .description { white-space: pre-wrap; background: #fafafa;
                      border-left: 3px solid #2e7d32; padding: 8px 12px; border-radius: 0 4px 4px 0; }

                    .photos { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 4px; }
                    .photos img { width: 150px; height: 110px; object-fit: cover;
                      border: 1px solid #ccc; border-radius: 4px; }

                    .note { margin: 6px 0; padding: 6px 10px;
                      border-left: 3px solid #a5d6a7; background: #f9fbe7; }
                    .note-author { font-weight: 600; }

                    .field-box { border: 1px solid #bbb; border-radius: 4px;
                      height: 60px; margin-bottom: 8px; }

                    .signature-block { margin-top: 48px; }
                    .sig-row { display: flex; gap: 80px; }
                    .sig-label { font-size: .85rem; color: #666; margin-bottom: 32px; }
                    .sig-line { border-top: 1px solid #555; width: 220px; padding-top: 4px;
                      font-size: .8rem; color: #888; }

                    .vat-box { border: 2px dashed #f57c00; padding: 14px 16px;
                      background: #fff8e1; color: #e65100; border-radius: 4px;
                      margin: 12px 0; font-size: .9rem; }

                    .print-btn {
                      position: fixed; top: 16px; right: 16px;
                      background: #2e7d32; color: white; border: none;
                      padding: 9px 22px; border-radius: 4px; cursor: pointer;
                      font-size: .9rem; font-weight: 600; box-shadow: 0 2px 6px rgba(0,0,0,.2);
                    }
                    .print-btn:hover { background: #1b5e20; }

                    @page  { margin: 18mm 20mm; }
                    @media print {
                      body { padding: 0; }
                      .print-btn { display: none; }
                    }
                  </style>
                </head>
                <body>
                  <button class="print-btn" onclick="window.print()">Ispis / PDF</button>
                  <h1>%s</h1>
                  %s
                </body>
                </html>
                """.formatted(title, orderNumber, title, body);
    }

    private String costTable(Integer km, Double workH, Integer workers, Double totalH, BigDecimal material) {
        return """
                <table class="cost">
                  <tr><th>Radni sati</th><td>%s</td></tr>
                  <tr><th>Broj radnika</th><td>%s</td></tr>
                  <tr><th>Ukupno sati</th><td>%s</td></tr>
                  <tr><th>Kilometri</th><td>%s km</td></tr>
                  <tr><th>Materijal</th><td>%s EUR</td></tr>
                </table>
                """.formatted(
                workH != null ? workH : "-",
                workers != null ? workers : "-",
                totalH != null ? totalH : "-",
                km != null ? km : "-",
                material != null ? material : "-");
    }

    private String photoGrid(Order o) {
        if (o.getPhotos().isEmpty()) return "<p><em>Nema fotografija.</em></p>";
        StringBuilder sb = new StringBuilder("<div class=\"photos\">");
        o.getPhotos().forEach(p ->
                sb.append("<img src=\"").append(baseUrl).append(p.getUrl()).append("\" alt=\"foto\">"));
        sb.append("</div>");
        return sb.toString();
    }

    private String statusLabel(Order.Status s) {
        return switch (s) {
            case DRAFT      -> "Nacrt";
            case PENDING    -> "Na čekanju";
            case IN_PROGRESS -> "U tijeku";
            case RESOLVED   -> "Riješen";
            case CANCELLED  -> "Otkazan";
        };
    }

    private String branch(Order o) { return o.getBranch() != null ? o.getBranch().getName() : "-"; }
    private String client(Order o) { return o.getClient() != null ? o.getClient().getName() : "-"; }
    private String s(String v)     { return v != null ? v : "-"; }
    private String today()         { return LocalDate.now().format(DATE_FMT); }
}
