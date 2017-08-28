import React, { Component } from 'react';
import './App.css';
import Upload from "./Upload";
import Compare from "./Compare";
import Waiting from "./Waiting";
import Error from "./Error";

class App extends Component {
  constructor(props) {
    super(props);
    this.props.appState.setComponent(this);
    this.state = {
      waiting: false,
      complete: false,
      from: null,
      to: null,
      error: null,
      serverSideError: false
    };
  }

  render() {
    return (
      <div className="App">
        {(this.state.error !== null) ?
            <Error message={this.state.error} serverSide={this.state.serverSideError} /> : "" }
        <Upload handler={this.props.appState.handleUpload.bind(this.props.appState)}/>
        {(this.state.waiting) ? <Waiting /> : ""}
        {(this.state.completed) ? <Compare from={this.state.from} to={this.state.to} /> : ""}
      </div>
    );
  }
}

export default App;
