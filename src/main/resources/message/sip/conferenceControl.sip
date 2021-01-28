MESSAGE sip:${remote_num}@${remote_ip} SIP/2.0
Via: SIP/2.0/UDP ${local_ip}:${local_port};rport;branch=z9hG4bKPj4e1932ba5bdb431d8c4a991b19b8008d
Max-Forwards: 70
From: "${local_num}" <sip:${local_num}@${local_ip}:${local_port}>
To: <sip:${remote_num}@${remote_ip}:${remote_port}>
Call-ID: 2d3827668c404047aa7f730cea30c63a
CSeq: ${cseq} MESSAGE
User-Agent: ${user_agent}
Content-Type: ${content_type}
Content-Length: ${content_length}

$(body)`${local_num} $(local_num)`1234` $(kk)`${local_num}` `
${body}