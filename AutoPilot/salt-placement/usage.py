from kmeans_icicle_clusterer import KMeansIcicleClusterer

test_data = [173,236,279,476,403,451,265]

kmeans_icicle_clusterer = KMeansIcicleClusterer(test_data)
print(kmeans_icicle_clusterer.cluster())
