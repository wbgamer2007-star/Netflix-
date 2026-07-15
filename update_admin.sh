#!/bin/bash
sed -i "s/currentTab === 'content'/currentTab === 'add'/g" admin.html
sed -i "s/currentTab === 'settings'/currentTab === 'settings'/g" admin.html
