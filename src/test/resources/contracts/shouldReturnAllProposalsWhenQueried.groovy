import org.springframework.cloud.contract.spec.Contract
Contract.make {
    description "should return info when queried"
    request{
        method 'GET'
        url '/info'
    }
    response {
        body("C4P v0.0.0")
        status 200
    }
}