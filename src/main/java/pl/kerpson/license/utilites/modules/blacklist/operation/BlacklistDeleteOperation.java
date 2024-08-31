package pl.kerpson.license.utilites.modules.blacklist.operation;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import pl.kerpson.license.utilites.MSecrets;
import pl.kerpson.license.utilites.http.HttpBuilder;
import pl.kerpson.license.utilites.modules.Operation;
import pl.kerpson.license.utilites.modules.OperationResult;
import pl.kerpson.license.utilites.status.StatusCode;
import pl.kerpson.license.utilites.status.StatusParser;

public class BlacklistDeleteOperation implements Operation<OperationResult<Boolean>> {

  private final String url;
  private final MSecrets secrets;

  public BlacklistDeleteOperation(String url, MSecrets secrets) {
    this.url = url;
    this.secrets = secrets;
  }

  private HttpBuilder prepareRequest() {
    return HttpBuilder.delete()
        .url(this.url)
        .bearer(this.secrets.getToken());
  }

  @Override
  public OperationResult<Boolean> complete() {
    try {
      HttpResponse<String> response = this.prepareRequest().sync();
      StatusCode statusCode = StatusParser.parse(response);
      if (!statusCode.isOk()) {
        return new OperationResult<>(false, statusCode.getThrowable());
      }

      return new OperationResult<>(true, null);
    } catch (Exception exception) {
      return new OperationResult<>(false, exception);
    }
  }

  @Override
  public CompletableFuture<OperationResult<Boolean>> completeAsync() {
    return prepareRequest().async().thenApply(response -> {
      StatusCode statusCode = StatusParser.parse(response);
      if (!statusCode.isOk()) {
        return new OperationResult<>(false, statusCode.getThrowable());
      }

      return new OperationResult<>(true, null);
    }).exceptionally(exception -> new OperationResult<>(false, exception));
  }
}
