# Production Deployment Checklist

## Pre-Deployment

### Security
- [ ] Add authentication (JWT/OAuth2/API Keys)
- [ ] Configure HTTPS/SSL certificates
- [ ] Set strong passwords for all services
- [ ] Review and update CORS allowed origins
- [ ] Enable firewall rules
- [ ] Move API keys to secrets manager
- [ ] Review SecurityConfig.java settings
- [ ] Disable Swagger UI in production
- [ ] Set secure session configuration

### Configuration
- [ ] Set SPRING_PROFILES_ACTIVE=prod
- [ ] Set proper download directory with sufficient space
- [ ] Configure proper log directory
- [ ] Set appropriate file retention hours
- [ ] Configure rate limiting
- [ ] Set max concurrent downloads based on server capacity
- [ ] Configure proper thread pool sizes

### Infrastructure
- [ ] Provision sufficient disk space (minimum 10GB)
- [ ] Set up log rotation
- [ ] Configure backup strategy
- [ ] Set up monitoring (Prometheus/Grafana)
- [ ] Configure alerts for:
  - [ ] High CPU usage
  - [ ] High memory usage
  - [ ] Low disk space
  - [ ] High error rates
  - [ ] Service downtime

### Dependencies
- [ ] Verify FFmpeg is installed
- [ ] Verify yt-dlp is installed and updated
- [ ] Test yt-dlp with sample videos
- [ ] Verify YouTube API key is valid
- [ ] Check API quota limits

## Deployment

### Docker Deployment
- [ ] Build Docker image: `docker-compose build`
- [ ] Test image locally: `docker-compose up`
- [ ] Push to container registry (if applicable)
- [ ] Deploy to production environment
- [ ] Verify health check: `curl https://server_url/api/v1/health`

### Traditional Deployment
- [ ] Build JAR: `mvn clean package`
- [ ] Copy JAR to production server
- [ ] Create systemd service file
- [ ] Start service
- [ ] Enable service on boot
- [ ] Verify service is running

### Post-Deployment
- [ ] Test all API endpoints
- [ ] Verify file uploads work
- [ ] Verify file downloads work
- [ ] Check logs for errors
- [ ] Monitor metrics
- [ ] Test rate limiting
- [ ] Verify cleanup job runs
- [ ] Load test with expected traffic

## Monitoring Setup

### Metrics to Monitor
- [ ] Request rate
- [ ] Error rate
- [ ] Conversion success/failure rate
- [ ] Conversion duration
- [ ] Disk usage
- [ ] CPU usage
- [ ] Memory usage
- [ ] Active downloads count

### Alerts
- [ ] Service down
- [ ] Error rate > threshold
- [ ] Disk usage > 80%
- [ ] Memory usage > 80%
- [ ] Response time > threshold
- [ ] YouTube API quota exceeded

## Maintenance

### Regular Tasks
- [ ] Daily: Check error logs
- [ ] Daily: Monitor disk usage
- [ ] Weekly: Review metrics and performance
- [ ] Weekly: Update yt-dlp: `pip3 install --upgrade yt-dlp`
- [ ] Monthly: Review and rotate API keys
- [ ] Monthly: Review security logs
- [ ] Quarterly: Update dependencies
- [ ] Quarterly: Security audit

### Backup Strategy
- [ ] Define backup schedule
- [ ] Test restore procedure
- [ ] Document backup locations
- [ ] Set retention policy

## Scaling Considerations

### Vertical Scaling
- [ ] Monitor resource usage trends
- [ ] Plan for capacity increases
- [ ] Test with increased limits

### Horizontal Scaling
- [ ] Use shared storage for downloads (S3)
- [ ] Implement distributed caching
- [ ] Use message queue for jobs
- [ ] Configure load balancer
- [ ] Test failover scenarios

## Documentation
- [ ] Document API endpoints
- [ ] Document deployment process
- [ ] Document rollback process
- [ ] Document incident response
- [ ] Create runbook for common issues
- [ ] Document escalation procedures

## Compliance & Legal
- [ ] Review YouTube Terms of Service
- [ ] Add privacy policy
- [ ] Add terms of service
- [ ] Document data retention policy
- [ ] Review copyright considerations

## Emergency Procedures

### Rollback Plan
1. [ ] Keep previous version available
2. [ ] Document rollback steps
3. [ ] Test rollback in staging
4. [ ] Define rollback triggers

### Incident Response
1. [ ] Define severity levels
2. [ ] Create escalation matrix
3. [ ] Document communication plan
4. [ ] Prepare incident templates

## Performance Benchmarks

Before deployment, establish baselines:
- [ ] Average conversion time for MP3
- [ ] Average conversion time for MP4
- [ ] Maximum concurrent conversions
- [ ] Average response time
- [ ] Memory usage under load
- [ ] CPU usage under load

## Sign-Off

- [ ] Development team approval
- [ ] Security review completed
- [ ] Infrastructure team approval
- [ ] Documentation reviewed
- [ ] Stakeholder approval

---

**Deployment Date:** _______________
**Deployed By:** _______________
**Version:** _______________
**Notes:** _______________
