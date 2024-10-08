package pl.kerpson.license.utilites.modules.license.operation;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import pl.kerpson.license.utilites.MSecrets;
import pl.kerpson.license.utilites.http.HttpBuilder;
import pl.kerpson.license.utilites.modules.Operation;
import pl.kerpson.license.utilites.modules.OperationResult;
import pl.kerpson.license.utilites.modules.license.basic.License;
import pl.kerpson.license.utilites.modules.license.basic.LicenseReader;
import pl.kerpson.license.utilites.status.StatusCode;
import pl.kerpson.license.utilites.status.StatusParser;

public class LicenseGetOperation implements Operation<OperationResult<License>> {

  private final String url;
  private final MSecrets secrets;

  public LicenseGetOperation(String url, MSecrets secrets) {
    this.url = url;
    this.secrets = secrets;
  }

  private HttpBuilder prepareRequest() {
    return HttpBuilder.get()
        .url(this.url)
        .bearer(this.secrets.getToken());
  }

  @Override
  public OperationResult<License> complete() {
    try {
      HttpResponse<String> response = this.prepareRequest().sync();
      StatusCode statusCode = StatusParser.parse(response);
      if (!statusCode.isOk()) {
        return new OperationResult<>(null, statusCode.getThrowable());
      }

      return new OperationResult<>(LicenseReader.readLicense(response), null);
    } catch (Exception exception) {
      return new OperationResult<>(null, exception);
    }
  }

  @Override
  public CompletableFuture<OperationResult<License>> completeAsync() {
    return prepareRequest().async()
        .thenApply(response -> {
          StatusCode statusCode = StatusParser.parse(response);
          if (!statusCode.isOk()) {
            return new OperationResult<License>(null, statusCode.getThrowable());
          }

          return new OperationResult<>(LicenseReader.readLicense(response), null);
        })
        .exceptionally(exception -> new OperationResult<>(null, exception));
  }
}
