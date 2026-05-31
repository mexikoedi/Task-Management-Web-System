/**
 * Diese Datei definiert die Hauptkomponente der Angular-Anwendung.
 * Sie legt die grundlegende Struktur und das Routing für die App fest.
 * Die Komponente ist mit @Component dekoriert, was den Selektor, das Template und die Styles für die Komponente angibt.
 * Der RouterOutlet wird importiert, um das Routing innerhalb der Anwendung zu ermöglichen,
 * sodass verschiedene Komponenten basierend auf der URL angezeigt werden können.
 * Die title-Eigenschaft ist in der Komponentenklasse definiert, die im Template verwendet werden kann,
 * um den Titel der Anwendung anzuzeigen.
 */
import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
})
export class AppComponent {
  title = 'TMWS';
}
