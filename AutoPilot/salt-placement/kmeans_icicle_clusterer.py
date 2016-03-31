from kmeans import kmeans_euclid

class KMeansIcicleClusterer(object):
    x_pos = []
    bracketed = []

    def __init__(self, x_pos):
        self.x_pos = x_pos
        for pos in x_pos:
            self.bracketed.append([pos])
        print self.bracketed

    def cluster(self):
        num_pos = len(self.x_pos)
        # Decide how many centroids based on number of icicles.
        k = 1
        if num_pos > 20:
            k = 3
        elif num_pos > 5:
            k = 2
        # Choose starting centroid positions.  
        min_pos = min(self.x_pos)
        max_pos = max(self.x_pos)
        div = (max_pos - min_pos) / (2*k)
        centroids = []
        for i in xrange(k):
            centroids.append([min_pos+div*(i*2+1)])
        print centroids
        # Run the clustering algorithm.
        kme = kmeans_euclid(self.bracketed, centroids, 0.1)
        # Return the centroids.
        return kme[0]

