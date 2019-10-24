#!/usr/bin/env bash

# Requires the installation of P6 Sync, usage examples:
# - Pull modifications: ./p6sync-update-local.sh Dev-Choucri -pull
# - Push modifications: ./p6sync-update-local.sh Dev-Choucri -push
p6sync -app p6_demo -debug -instance $1 -approot . $2 force -p6proxyoverride http://localhost:8480
