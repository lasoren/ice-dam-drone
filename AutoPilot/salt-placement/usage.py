from kmeans_icicle_clusterer import KMeansIcicleClusterer

test_data = [362,331,383,213,384,340,356,321,357]

kmeans_icicle_clusterer = KMeansIcicleClusterer(test_data)
print(kmeans_icicle_clusterer.cluster())

