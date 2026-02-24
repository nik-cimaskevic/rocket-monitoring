FROM alpine:3.19

WORKDIR /app

COPY rockets /app/rockets

RUN chmod +x /app/rockets

ENTRYPOINT ["/app/rockets"]