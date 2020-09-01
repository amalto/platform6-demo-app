#!/usr/bin/env bash

# Requires the installation of P6 Sync, usage examples:
# - Pull modifications: ./p6sync-update-local.sh damp-pine-3678 -pull
# - Push modifications: ./p6sync-update-local.sh damp-pine-3678 -push
p6sync -app p6_demo -debug -instance $1 -approot . $2 update -p6proxyoverride http://localhost:8480
