import io.grpc.*;

public class LoggingInterceptor  implements ServerInterceptor {
// Listener обьект через который нажимаются кнопки ( завершения,  на получение сообещния и тд
    // мы говорим что создав этот обьект (где то в коде есть  логика использвания ServerCall.Listener где )
    // переписываем возращаемый метод которй gRPC дернет по окончанию метода
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        String methodName = call.getMethodDescriptor().getFullMethodName();

        System.out.printf("method = %s started%n", methodName);
        long startTime = System.currentTimeMillis();

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onComplete() {
                long duration = System.currentTimeMillis() - startTime;
                System.out.printf("method = %s, duration = %d ms%n", methodName, duration);
                super.onComplete();
            }

            @Override
            public void onCancel() {
                long duration = System.currentTimeMillis() - startTime;
                System.out.printf("method = %s, duration = %d  ms CANCELLED%n", methodName, duration);
                super.onCancel();
            }

        };


    }
}

