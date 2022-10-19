#!/usr/bin/env bash
echo '=====开始安装蘑菇博客环境====='
docker build -f ./gateway.Dockerfile  -t mogu_gateway .
docker build -f ./picture.Dockerfile  -t mogu_picture .
docker build -f ./sms.Dockerfile  -t mogu_sms .
docker build -f ./admin.Dockerfile  -t mogu_admin .
docker build -f ./spider.Dockerfile  -t mogu_spider .
docker build -f ./monitor.Dockerfile  -t mogu_monitor .
docker build -f ./search.Dockerfile  -t mogu_search .
docker build -f ./web.Dockerfile  -t mogu_web .