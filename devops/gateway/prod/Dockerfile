FROM nginx:latest

COPY config.nginx /etc/nginx/conf.d/default.conf

ENTRYPOINT ["/docker-entrypoint.sh"]

EXPOSE 80

STOPSIGNAL SIGQUIT

CMD ["nginx", "-g", "daemon off;"]