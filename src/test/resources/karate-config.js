function fn() {
  var System = Java.type('java.lang.System');
  return {
    baseUrl: karate.properties['monitoring.baseUrl'] || System.getenv('MONITORING_BASE_URL') || 'http://localhost:8091',
    jwt: karate.properties['monitoring.jwt'] || System.getenv('MONITORING_JWT'),
    requestId: Number(System.getenv('MONITORING_REQUEST_ID')),
    technicianId: Number(System.getenv('MONITORING_TECHNICIAN_ID')),
    reportId: Number(System.getenv('MONITORING_REPORT_ID'))
  };
}
