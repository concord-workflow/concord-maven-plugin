
# Concord Maven Plugin

The **Concord** Maven plugin is a tool designed to help developers maintain consistency in dependency versions and scopes within projects that use Concord plugins. 
By validating dependencies against expected runtime versions and scopes, this plugin reduces potential issues during runtime and ensures compliance with Concord's dependency requirements.

## Features

- **Version Validation**: Compares the versions of project dependencies against expected runtime versions, flagging any mismatches.
- **Scope Validation**: Enforces the correct scope (`provided`) for specific Concord dependencies, ensuring they are not bundled unnecessarily into final artifacts.

## Usage

### Adding the Plugin to Your Project

Include the plugin in your project's `pom.xml` file:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>dev.ybrig.concord</groupId>
            <artifactId>concord-maven-plugin-ng</artifactId>
            <version>0.0.29</version>
            <configuration>
                <concordVersion>2.19.0</concordVersion>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>sisu-index</goal>
                        <goal>verify</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Example Output

If violations are detected, the plugin will produce warnings in the following format:

```plaintext
[WARNING] The dependency 'com.walmartlabs.concord:concord-sdk:2.17.0' has an invalid version.
          Expected version: '2.18.0'. Please update the version in your POM to match the required version.

[WARNING] Some dependencies are expected to be in 'provided' scope.
          Please ensure the following dependencies are set to '<scope>provided</scope>' in your POM:
          * com.walmartlabs.concord:concord-sdk:2.17.0
```

## Benefits

Using the Concord Dependency Validator plugin helps to:

- **Maintain Consistency**: Ensures dependencies are aligned with Concord’s runtime environment requirements.
- **Reduce Errors**: Minimizes runtime issues due to version mismatches or incorrectly scoped dependencies.

