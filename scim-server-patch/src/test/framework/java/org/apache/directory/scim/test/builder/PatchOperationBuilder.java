/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.directory.scim.test.builder;

import org.apache.directory.scim.spec.protocol.data.PatchOperation;
import org.apache.directory.scim.spec.protocol.data.PatchOperation.Type;
import org.apache.directory.scim.spec.protocol.data.PatchOperationPath;
import org.apache.directory.scim.spec.protocol.filter.FilterParseException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PatchOperationBuilder {
  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(PatchOperation data) {
    return new Builder(data);
  }

  public static final class Builder {

    private Type operation;
    private PatchOperationPath path = null;
    private Object value = null;

    private Builder() {
    }

    private Builder(PatchOperation initialData) {
      this.operation = initialData.getOperation();
      this.path = initialData.getPath();
      this.value = initialData.getValue();
    }

    public Builder operation(Type operation) {
      this.operation = operation;
      return this;
    }

    public Builder path(String path)
      throws FilterParseException {
      return this.path(new PatchOperationPath(path));
    }

    public Builder path(PatchOperationPath path) {
      this.path = path;
      return this;
    }

    public Builder value(Object value) {
      this.value = value;
      return this;
    }

    public PatchOperation build() {
      final PatchOperation patchOperation = new PatchOperation();

      patchOperation.setPath(path);
      patchOperation.setOperation(operation);
      patchOperation.setValue(value);

      return patchOperation;
    }
  }
}
