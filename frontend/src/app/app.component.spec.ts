/**
 * Diese Datei enthält Unit-Tests für die AppComponent der Angular-Anwendung.
 * Sie verwendet das Angular-Testframework, um eine Test-Suite zu erstellen, die überprüft,
 * ob die AppComponent erfolgreich erstellt wird und ob sie die richtige title-Eigenschaft hat.
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent', (): void => {
  beforeEach(async (): Promise<void> => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
    }).compileComponents();
  });

  it('Sollte erstellen', (): void => {
    const fixture: ComponentFixture<AppComponent> = TestBed.createComponent(AppComponent);
    const component: AppComponent = fixture.componentInstance;
    expect(component).toBeTruthy();
  });

  it(`Sollte den Titel 'TMWS' haben`, (): void => {
    const fixture: ComponentFixture<AppComponent> = TestBed.createComponent(AppComponent);
    const component: AppComponent = fixture.componentInstance;
    expect(component.title).toEqual('TMWS');
  });
});
