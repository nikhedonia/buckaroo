package com.loopperfect.buckaroo;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.reactivex.*;
import io.reactivex.Observable;

import java.util.*;
import java.util.stream.Stream;

public final class AsyncDependencyResolver {

    private AsyncDependencyResolver() {

    }

    private static Single<ImmutableMap<RecipeIdentifier, SemanticVersion>> step(
        final RecipeSource recipeSource,
        final ImmutableMap<RecipeIdentifier, SemanticVersion> resolved,
        final Dependency next,
        final ResolutionStrategy strategy) {

        Preconditions.checkNotNull(recipeSource);
        Preconditions.checkNotNull(resolved);
        Preconditions.checkNotNull(next);
        Preconditions.checkNotNull(strategy);

        if (resolved.containsKey(next.project)) {
            final SemanticVersion resolvedVersion = resolved.get(next.project);
            return next.requirement.isSatisfiedBy(resolvedVersion) ?
                Single.just(ImmutableMap.copyOf(resolved)) :
                Single.error(new DependencyResolutionException(
                    next.project.encode() + "@" + resolvedVersion.encode() + " does not satisfy " + next.encode()));
        }

        return recipeSource.fetch(next.project)
            .flatMap(recipe -> {

                final Stream<Single<ImmutableMap<RecipeIdentifier, SemanticVersion>>> candidateStream = recipe.versions.entrySet()
                    .stream()
                    .filter(x -> next.requirement.isSatisfiedBy(x.getKey()))
                    .sorted(Comparator.comparing(Map.Entry::getKey))
                    .map(entry -> {

                        final ImmutableMap<RecipeIdentifier, SemanticVersion> nextResolved = MoreMaps.with(
                            resolved,
                            next.project, entry.getKey());

                        final ImmutableList<Dependency> nextDependencies = new ImmutableList.Builder<Dependency>()
                            .addAll(entry.getValue().dependencies.orElse(DependencyGroup.of()).entries())
                            .build();

                        return resolve(
                            recipeSource,
                            nextResolved,
                            nextDependencies,
                            strategy);
                    });

                return MoreObservables.findMax(
                    MoreObservables.skipErrors(Observable.fromIterable(candidateStream::iterator)),
                    Comparator.comparing(strategy::score))
                    .toSingle()
                    .onErrorResumeNext(error -> Single.error(new DependencyResolutionException("Could not satisfy " + next, error)));
            });
    }

    private static Single<ImmutableMap<RecipeIdentifier, SemanticVersion>> resolve(
        final RecipeSource recipeSource,
        final ImmutableMap<RecipeIdentifier, SemanticVersion> resolved,
        final ImmutableList<Dependency> dependencies,
        final ResolutionStrategy strategy) {

        Preconditions.checkNotNull(recipeSource);
        Preconditions.checkNotNull(resolved);
        Preconditions.checkNotNull(dependencies);
        Preconditions.checkNotNull(strategy);

        return dependencies.stream().reduce(
            Single.just(resolved),
            (state, next) ->
                state.flatMap(x -> step(recipeSource, x, next, strategy)),
            (x, y) -> Single.zip(x, y, MoreMaps::merge));
    }

    public static Single<ImmutableMap<RecipeIdentifier, SemanticVersion>> resolve(
        final RecipeSource recipeSource,
        final ImmutableList<Dependency> dependencies) {
        return resolve(recipeSource, ImmutableMap.of(), dependencies, SumResolutionStrategy.of());
    }
}
