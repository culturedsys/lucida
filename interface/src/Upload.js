/**
 * The upload control, which includes a form to allow for uploading two
 * files.
 */

import React, { Component } from 'react';
import './Upload.css';

class Upload extends Component {
  render() {
    return (
        <div className="Upload">
          <form ref={form => this.form = form}>
            <div className="row">
              <div className="form-group col-md-6">
                <label htmlFor="fromFile">Original document</label>
                <input type="file" className="Upload-file" name="fromFile" />
              </div>
              <div className="form-group col-md-6">
                <label htmlFor="toFile">Modified document</label>
                <input type="file" className="Upload-file" name="toFile" />
              </div>
            </div>
            <div className="form-group col-md-12 row">
              <button onClick={(e) => this.doUpload(e)}>Compare files</button>
            </div>
          </form>
        </div>
    );
  }

  doUpload(e) {
    e.preventDefault();
    this.props.handler(new FormData(this.form));
  }
}

export default Upload;