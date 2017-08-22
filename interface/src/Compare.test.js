/**
 * Tests for the comparison component
 */

import React from 'react';
import ReactDOM from 'react-dom';
import Compare, {DocumentTree} from './Compare.js';

test("DocumentTree with no children does not contain a nested list", () => {
  let div = document.createElement('div');
  let doc = {
    description: 'description',
    change: 'changed',
    children: []
  }
  ReactDOM.render(<DocumentTree document={doc} />, div);

  expect(div.querySelectorAll('ul ul').length).toBe(0);
})

test("DocumentTree with children contains a nested list", ()  => {
  let div = document.createElement('div');
  let doc = {
    description: 'description',
    change: 'changed',
    children: [
      {
        description: 'first child',
        change: 'unchanged',
        children: []
      },
      {
        description: 'second child',
        change: 'inserted',
        children: []
      }
    ]
  }
  ReactDOM.render(<DocumentTree document={doc} />, div);

  expect(div.querySelectorAll('ul ul').length).toBe(1);
});

test("Compare contains two top-level lists", () => {
  let div = document.createElement('div');
  let doc = {
    description: 'description',
    change: 'changed',
    children: []
  }

  ReactDOM.render(<Compare from={doc} to={doc} />, div);

  expect(div.querySelectorAll('div > ul').length).toBe(2);
});