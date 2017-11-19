// @flow
import React, { Component } from 'react';
import { connect } from 'react-redux';
import Aerospike from '../components/Aerospike';

const mapStateToProps = state => ({
  label: "test",
})

const mapDispatchToProps = dispatch => ({
  click: () => {},
})

export default connect(mapStateToProps, mapDispatchToProps)(Aerospike);
