/**
 * A component that shows two document structures side by side.
 */

import React, {Component} from 'react';
import './Compare.css';

class Compare extends Component {
  render() {
    return (
        <div>
          <div className="Compare row">
            <DocumentTree document={this.props.from} />
            <DocumentTree document={this.props.to} />
          </div>
          <div className="Compare-legend row">
            <div className="col-md-4">
              <span className="Compare-legend-changed">&nbsp;</span>
              Changed
            </div>
            <div className="col-md-4">
              <span className="Compare-legend-inserted">+</span>
              Inserted
            </div>
            <div className="col-md-4">
              <span className="Compare-legend-deleted">&#x2212;</span>
              Deleted
            </div>
          </div>
        </div>
    );
  }
}

class DocumentTree extends Component {
  render() {
    return (
        <ul className="DocumentTree DocumentTree-children col-md-6">
          { this.element(this.props.document) }
        </ul>
    );
  }

  element(doc, index) {
    let children = doc.children;
    let childrenElement = <span />;

    if (children.length > 0) {
      childrenElement = <ul className="DocumentTree-children">{children.map((child) => this.element(child))}</ul>
    }

    return (
        <li className={"DocumentTree-element DocumentTree-" + doc.change} key={index}>
          <div className="DocumentTree-description">{doc.description}</div>
          { childrenElement }
        </li>
    );
  }
}

export { DocumentTree, Compare as default };
