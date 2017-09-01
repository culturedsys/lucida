import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/css/bootstrap-theme.css';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import AppState from './AppState';
import App from './App';
import About from './About';




function main() {
  let app;

  if (window.location.hash === '#about')
    app = <About />;
  else {
    const state = new AppState(window.fetch.bind(window),
        new URL("requests/", window.location), 500);
    app = <App appState={state}/>;
  }

  ReactDOM.render(app, document.getElementById('root'));
}

window.addEventListener('hashchange', main);
main();