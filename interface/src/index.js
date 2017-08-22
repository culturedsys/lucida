import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap/dist/css/bootstrap-theme.css';
import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import AppState from './AppState';
import App from './App';


const state = new AppState(window.fetch.bind(window), new URL("requests/", window.location), 500);

ReactDOM.render(<App appState={state} />, document.getElementById('root'));
