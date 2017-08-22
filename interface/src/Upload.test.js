/**
 * Tests for the upload control.
 */

import React from 'react';
import ReactDOM from 'react-dom'
import Upload from './Upload.js'

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Upload />, div);
});

it('should have two file controls', () => {
  const div = document.createElement('div');
  ReactDOM.render(<Upload/>, div)
  const fileInputs = div.querySelectorAll('input[type=file]');
  expect(fileInputs.length).toBe(2);
});


