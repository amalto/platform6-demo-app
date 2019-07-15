#!/usr/bin/env bash

# Requires the installation of P6 Sync, usage examples:
# - Pull modifications: ./p6sync-update.sh Dev-Choucri -pull
# - Push modifications: ./p6sync-update.sh Dev-Choucri -push
p6sync -app p6_demo -debug -instance $1 -approot . $2 update
