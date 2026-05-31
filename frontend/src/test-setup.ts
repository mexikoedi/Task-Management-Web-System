/**
 * Initialisiere die Angular-Testumgebung und richtete globale Konfigurationen für Tests ein.
 * Diese Datei wird typischerweise verwendet, um die Testumgebung zu konfigurieren, bevor Tests ausgeführt werden.
 * Sie importiert notwendige Module und initialisiert den Testbed für Angular-Tests.
 */
import { getTestBed } from '@angular/core/testing';
import { BrowserTestingModule, platformBrowserTesting } from '@angular/platform-browser/testing';

getTestBed().initTestEnvironment(BrowserTestingModule, platformBrowserTesting());
