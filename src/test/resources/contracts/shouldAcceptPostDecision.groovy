import org.springframework.cloud.contract.spec.Contract
[
    Contract.make {
        name "should accept POST with Proposal Decision rejected"
        request{
            method 'POST'
            url '/ABC/decision/'
            body([
                "approved": false,
            ])
            headers {
                contentType('application/json')
            }
        }
        response {
            status OK()
        }
    },
    Contract.make {
        name "should accept POST with Proposal Decision approved"
        request{
            method 'POST'
            url '/ABC/decision/'
            body([
                    "approved": true,
            ])
            headers {
                contentType('application/json')
            }
        }
        response {
            status OK()
        }
    }
]