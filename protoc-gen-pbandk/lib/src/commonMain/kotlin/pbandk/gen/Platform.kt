package pbandk.gen

import pbandk.gen.pb.CodeGeneratorRequest
import pbandk.gen.pb.CodeGeneratorResponse

internal interface Platform {
    fun stderrPrintln(str: String)
    fun stdinReadRequest(): CodeGeneratorRequest
    fun stdoutWriteResponse(resp: CodeGeneratorResponse)

    fun serviceGenerator(cliParams: Map<String, String>): ServiceGenerator?
}

internal expect fun getPlatform(): Platform