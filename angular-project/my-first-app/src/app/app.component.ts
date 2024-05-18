import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'my-first-app';
  dynamicthing = '123';
  updatething = () => {
    console.log(this);
    this.dynamicthing='haha!'
  };
}
