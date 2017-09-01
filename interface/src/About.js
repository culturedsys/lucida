/**
 * A component that displays the about page.
 */

import React, { Component } from 'react';

export default class About extends Component {
  render() {
    return <div>
      <h1>About Lucida</h1>
      <p>
        Lucida is a small system that shows the difference in structure between
        two documents. It is intended to show the viability (and perhaps even
        the value) of using document structure detection to produce tools for
        writers and editors.
      </p>
      <p>
        Lucida was written by <a href="https://www.timfisken.org">Tim
        Fisken</a>, and <a href="https://github.com/culturedsys/lucida">source
        code is available on GitHub</a>.
      </p>
      <p>
        Lucida stores documents you upload for some short period of time, and
        could serve them to other users (if they happen to see or guess the id
        used to identify them), so you should avoid uploading confidential
        documents.
      </p>
    </div>
  }
}