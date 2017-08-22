/**
 * A component that shows two document structures side by side.
 */

import React, {Component} from 'react';
import './Compare.css';

class Compare extends Component {
  render() {
    return (
        <div className="Compare row">
          <DocumentTree document={this.props.from} />
          <DocumentTree document={this.props.to} />
        </div>
    )
  }
}

class DocumentTree extends Component {
  render() {
    return (
        <ul className="DocumentTree DocumentTree-children col-md-6">
          { this.element(this.props.document) }
        </ul>
    )
  }

  element(doc, index) {
    let children = doc.children;
    let childrenElement = <span />;

    if (children.length > 0) {
      childrenElement = <ul className="DocumentTree-children">{children.map(this.element)}</ul>
    }

    return (<li className={"DocumentTree-element DocumentTree-" + doc.change} key={index}>
      <div className="DocumentTree-description">{doc.description}</div>
      { childrenElement }
    </li>)
  }
}

export { DocumentTree, Compare as default };
