import React, {Component} from 'react';

/**
 * Display a message formatted using the bootstrap "danger" style.
 */
export default class Error extends Component {
  render() {
    return <div className="alert alert-danger">
      {this.props.message}
    </div>
  }
}