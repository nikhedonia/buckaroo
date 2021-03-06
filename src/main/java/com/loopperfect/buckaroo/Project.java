package com.loopperfect.buckaroo;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;

import java.util.Objects;
import java.util.Optional;

public final class Project {

    public final String name;
    public final Optional<String> license;
    public final DependencyGroup dependencies;

    private Project(final String name, final Optional<String> license, final DependencyGroup dependencies) {

        this.name = Preconditions.checkNotNull(name);
        this.license = Preconditions.checkNotNull(license);
        this.dependencies = Preconditions.checkNotNull(dependencies);
    }

    public Project addDependency(final Dependency dependency) {
        Preconditions.checkNotNull(dependency);
        return new Project(name, license, dependencies.addDependency(dependency));
    }

    public Project addDependencyGroup(final DependencyGroup dependencyGroup) {
        Preconditions.checkNotNull(dependencyGroup);
        return new Project(name, license, dependencies.addDependencyGroup(dependencyGroup));
    }

    public Project removeDependency(final RecipeIdentifier identifier) {
        Preconditions.checkNotNull(identifier);
        return new Project(name, license, dependencies.removeDependency(identifier));
    }

    @Override
    public boolean equals(final Object obj) {

        if (obj == null || !(obj instanceof Project)) {
            return false;
        }

        final Project other = (Project) obj;

        return Objects.equals(name, other.name) &&
            Objects.equals(license, other.license) &&
            Objects.equals(dependencies, other.dependencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, license, dependencies);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("name", name)
            .add("license", license)
            .add("dependencies", dependencies)
            .toString();
    }

    public static Project of(final String name, final Optional<String> license, final DependencyGroup dependencies) {
        return new Project(name, license, dependencies);
    }

    public static Project of(final String name) {
        return new Project(name, Optional.empty(), DependencyGroup.of());
    }

    public static Project of(final String name, final DependencyGroup dependencies) {
        return new Project(name, Optional.empty(), dependencies);
    }
}
