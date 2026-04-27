import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { MagnifyingGlassIcon, MapPinIcon, AdjustmentsHorizontalIcon } from '@heroicons/react/24/outline';
import { toast } from 'react-hot-toast';
import ProductCard from '../components/ProductCard';
import useGeolocation from '../hooks/useGeolocation';
import useDebounce from '../hooks/useDebounce';

interface InventoryItem {
  id: string;
  name: string;
  description: string;
  category: string;
  price: number;
  stockQuantity: number;
  storeName: string;
  storeId: string;
  latitude: number;
  longitude: number;
  imageUrl: string;
  unit: string;
  isAvailable: boolean;
  lastUpdated: string;
  distance: number;
  estimatedDeliveryMins: number;
}

const categories = ['All', 'GROCERY', 'SNACKS', 'GIFTS', 'BEVERAGES', 'HOUSEHOLD'];

export default function Home() {
  const { location, loading: locationLoading, error: locationError, requestLocation } = useGeolocation();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [radius, setRadius] = useState(5.0);
  const [sortBy, setSortBy] = useState('distance');

  const debouncedSearch = useDebounce(searchQuery, 400);

  const { data: items, isLoading, error } = useQuery({
    queryKey: ['items', location?.lat, location?.lng, radius, selectedCategory, debouncedSearch, sortBy],
    queryFn: async () => {
      if (!location) return [];

      const params = new URLSearchParams({
        lat: location.lat.toString(),
        lng: location.lng.toString(),
        radius: radius.toString(),
        sort: sortBy,
        page: '0',
        size: '50'
      });

      if (selectedCategory !== 'All') params.append('category', selectedCategory);
      if (debouncedSearch) params.append('query', debouncedSearch);

      const response = await fetch(`/api/v1/items/search?${params}`);
      if (!response.ok) throw new Error('Failed to fetch items');
      return response.json() as Promise<InventoryItem[]>;
    },
    enabled: !!location,
    staleTime: 2 * 60 * 1000, // 2 minutes
  });

  useEffect(() => {
    if (locationError) {
      toast.error('Unable to get your location. Please enable location services.');
    }
  }, [locationError]);

  const handleLocationChange = () => {
    requestLocation();
  };

  const getLocationDisplay = () => {
    if (locationLoading) return 'Detecting location...';
    if (location) return `📍 Delivering to ${location.lat.toFixed(4)}, ${location.lng.toFixed(4)}`;
    return '📍 Location not set';
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <h1 className="text-2xl font-bold text-gray-900">Antigravity Store</h1>
            <div className="flex items-center space-x-4">
              <div className="text-sm text-gray-600">{getLocationDisplay()}</div>
              <button
                onClick={handleLocationChange}
                className="text-emerald-600 hover:text-emerald-700 text-sm font-medium"
                disabled={locationLoading}
              >
                Change
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Search Bar */}
      <div className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search for groceries, snacks, gifts..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="block w-full pl-10 pr-3 py-4 border border-gray-300 rounded-lg focus:ring-emerald-500 focus:border-emerald-500 text-lg"
            />
          </div>
        </div>
      </div>

      {/* Category Chips */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex space-x-2 overflow-x-auto pb-2">
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => setSelectedCategory(category)}
                className={`px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap ${
                  selectedCategory === category
                    ? 'bg-emerald-100 text-emerald-800 border-2 border-emerald-500'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-3">
          <div className="flex items-center space-x-4">
            <div className="flex items-center space-x-2">
              <MapPinIcon className="h-4 w-4 text-gray-500" />
              <span className="text-sm text-gray-600">Radius:</span>
              <select
                value={radius}
                onChange={(e) => setRadius(Number(e.target.value))}
                className="text-sm border border-gray-300 rounded px-2 py-1"
              >
                <option value={1}>1 km</option>
                <option value={3}>3 km</option>
                <option value={5}>5 km</option>
                <option value={10}>10 km</option>
              </select>
            </div>
            <div className="flex items-center space-x-2">
              <AdjustmentsHorizontalIcon className="h-4 w-4 text-gray-500" />
              <span className="text-sm text-gray-600">Sort by:</span>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="text-sm border border-gray-300 rounded px-2 py-1"
              >
                <option value="distance">Distance</option>
                <option value="price_asc">Price: Low to High</option>
                <option value="price_desc">Price: High to Low</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Product Grid */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {isLoading ? (
          <div className="text-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-emerald-500 mx-auto"></div>
            <p className="mt-4 text-gray-600">Finding the best deals near you...</p>
          </div>
        ) : error ? (
          <div className="text-center py-12">
            <p className="text-red-600">Failed to load items. Please try again.</p>
          </div>
        ) : !location ? (
          <div className="text-center py-12">
            <MapPinIcon className="h-16 w-16 text-gray-400 mx-auto mb-4" />
            <h2 className="text-xl font-semibold text-gray-900 mb-2">Location Required</h2>
            <p className="text-gray-600 mb-4">Please enable location services to find items near you.</p>
            <button
              onClick={handleLocationChange}
              className="bg-emerald-600 text-white px-6 py-2 rounded-lg hover:bg-emerald-700"
            >
              Enable Location
            </button>
          </div>
        ) : items && items.length > 0 ? (
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6">
            {items.map((item) => (
              <ProductCard key={item.id} item={item} />
            ))}
          </div>
        ) : (
          <div className="text-center py-12">
            <p className="text-gray-600">No items found in your area. Try expanding your search radius.</p>
          </div>
        )}
      </main>
    </div>
  );
}