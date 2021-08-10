package org.apache.directory.scim.test.arguments.provider.args;

import static java.util.Objects.hash;
import static java.util.Objects.requireNonNull;
import static org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;
import org.apache.directory.scim.spec.resources.ScimResource;

public class PatchArgs<T extends ScimResource> {
  private final PatchOperationPath path;
  private final Type type;
  private final Object value;
  private final Function<T,Void> setter;
  private final Map<Function<T, ?>, Object> validations;

  public PatchArgs(final PatchOperationPath path, final Type type, final Object value,
                   final Function<T,Void> setter, final Map<Function<T, ?>, Object> validations) {
    this.path = path;
    this.type = requireNonNull(type, "type must not be null");
    this.value = value;
    this.setter = setter;
    this.validations = requireNonNull(validations, "validations must not be null");
  }

  public PatchOperationPath path() { return path; }
  public Type type() { return type; }
  public Object value() { return value; }
  public Map<Function<T, ?>, Object> validations() { return validations; }

  public void setter(final T source) {
    if(setter != null) {
      setter.apply(source);
    }
  }

  public void validate(final T actual) {
    validations().forEach((validate,expectedValue) -> assertThat(validate.apply(actual))
      .isEqualTo(expectedValue));
  }

  @Override
  public boolean equals(Object o) {
    if (this==o) {
      return true;
    }
    if (o==null || getClass()!=o.getClass()) {
      return false;
    }

    PatchArgs<?> that = (PatchArgs<?>) o;

    return Objects.equals(this.path(), that.path()) && Objects.equals(this.type(), that.type())
      && Objects.equals(this.value(), that.value()) && Objects.equals(this.validations(), that.validations());
  }

  @Override
  public int hashCode() {
    return hash(path(), type(), value(), validations());
  }

  @Override
  public String toString() {
    return path() != null ? type().name() + " : " + path() : type().name() + " : <target (path) omitted>";
  }

  public static <T extends ScimResource> PatchArgs.Builder<T> builder() {
    return new PatchArgs.Builder<>();
  }

  public static final class Builder<T extends ScimResource> {
    private PatchOperationPath path;
    private Type type;
    private Object value = null;
    private Function<T,Void> setter;
    private Map<Function<T, ?>, Object> validations;

    public Builder() {
    }

    public Builder<T> path(final String path) throws FilterParseException {
      return path(new PatchOperationPath(path));
    }

    public Builder<T> path(PatchOperationPath path) {
      this.path = path;
      return this;
    }

    public Builder<T> type(Type type) {
      this.type = type;
      return this;
    }

    public Builder<T> value(Object value) {
      this.value = value;
      return this;
    }

    public Builder<T> setter(Function<T, Void> setter) {
      this.setter = setter;
      return this;
    }

    public Builder<T> validate(Function<T, ?> validate, final Object expectedValue) {
      if(this.validations == null ) {
        this.validations = new HashMap<>();
      }

      this.validations.put(validate, expectedValue);
      return this;
    }

    public PatchArgs<T> build() {
      return new PatchArgs<T>(path, type,  value, setter, validations);
    }
  }
}
