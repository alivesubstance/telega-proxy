#!/usr/bin/env bash

curl -X POST \
    -H "Content-Type: application/json" \
    -d '{"chat_id": "239922878", "text": "q3.playground.ru:27961"}' \
    https://telega-proxy.appspot.com/bot735603182:AAF_WMAzBOa1vLxELNDt4EilNE_sHF3OG_4/sendMessage
