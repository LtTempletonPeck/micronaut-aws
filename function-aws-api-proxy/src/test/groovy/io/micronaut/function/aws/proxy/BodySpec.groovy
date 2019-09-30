package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext
import com.amazonaws.services.lambda.runtime.Context
import groovy.transform.Canonical
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Status
import io.reactivex.Single
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification


class BodySpec extends Specification {

    @Shared @AutoCleanup MicronautLambdaContainerHandler handler = MicronautLambdaContainerHandler.getAwsProxyHandler(
                ApplicationContext.build()
    )
    @Shared Context lambdaContext = new MockLambdaContext()

    void "test custom body POJO"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test custom body POJO - default to JSON"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo', HttpMethod.POST.toString())
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test custom body POJO with whole request"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo-and-request', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'

    }

    void "test custom body POJO - reactive types"() {
        given:
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder('/response-body/pojo-reactive', HttpMethod.POST.toString())
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
        builder.body('{"x":10,"y":20}')

        when:
        def response = handler.proxy(builder.build(), lambdaContext)

        then:
        response.statusCode == 201
        response.body == '{"x":10,"y":20}'
    }

    @Controller('/response-body')
    static class BodyController {

        @Post(uri = "/pojo")
        @Status(HttpStatus.CREATED)
        Point post(@Body Point data) {
            return data
        }

        @Post(uri = "/pojo-and-request")
        @Status(HttpStatus.CREATED)
        Point postRequest(HttpRequest<Point> request) {
            return request.body.orElse(null)
        }


        @Post(uri = "/pojo-reactive")
        @Status(HttpStatus.CREATED)
        Single<Point> post(@Body Single<Point> data) {
            return data
        }
    }

    @Canonical
    static class Point {
        Integer x,y
    }
}
