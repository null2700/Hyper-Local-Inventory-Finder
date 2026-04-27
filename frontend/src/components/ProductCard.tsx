import { useState } from 'react';
import { ClockIcon, MapPinIcon, MinusIcon, PlusIcon } from '@heroicons/react/24/outline';
import { toast } from 'react-hot-toast';

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

interface ProductCardProps {
  item: InventoryItem;
}

export default function ProductCard({ item }: ProductCardProps) {
  const [quantity, setQuantity] = useState(0);

  const handleAddToCart = async () => {
    if (quantity === 0) {
      setQuantity(1);
      toast.success(`${item.name} added to cart!`);
    } else {
      // Here you would typically update the cart
      toast.success(`Updated ${item.name} quantity to ${quantity + 1}`);
      setQuantity(prev => prev + 1);
    }
  };

  const handleQuantityChange = (delta: number) => {
    const newQuantity = quantity + delta;
    if (newQuantity >= 0 && newQuantity <= item.stockQuantity) {
      setQuantity(newQuantity);
      if (newQuantity === 0) {
        toast.success(`${item.name} removed from cart`);
      }
    }
  };

  const getStockStatus = () => {
    if (item.stockQuantity === 0) return { text: 'Out of stock', color: 'text-red-600' };
    if (item.stockQuantity <= 5) return { text: 'Low stock', color: 'text-orange-600' };
    return { text: 'In stock', color: 'text-green-600' };
  };

  const stockStatus = getStockStatus();
  const imageSrc = item.imageUrl?.startsWith('/images/')
    ? `http://localhost:8081${item.imageUrl}`
    : item.imageUrl;

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden hover:shadow-md transition-shadow">
      {/* Image */}
      <div className="aspect-square bg-gray-100 relative">
        {item.imageUrl ? (
          <img
            src={imageSrc}
            alt={item.name}
            className="w-full h-full object-cover"
            onError={(e) => {
              // Hide the broken image and show fallback
              e.currentTarget.style.display = 'none';
              const parent = e.currentTarget.parentElement;
              if (parent) {
                const fallback = parent.querySelector('.image-fallback') as HTMLElement;
                if (fallback) fallback.style.display = 'flex';
              }
            }}
          />
        ) : null}
        <div className="image-fallback w-full h-full flex items-center justify-center text-gray-400" style={{display: item.imageUrl ? 'none' : 'flex'}}>
          <div className="text-center">
            <div className="text-4xl mb-2">📦</div>
            <div className="text-sm">No Image</div>
          </div>
        </div>

        {/* Delivery Badge */}
        <div className="absolute top-2 left-2 bg-white bg-opacity-90 rounded-full px-2 py-1 text-xs font-medium text-gray-700 flex items-center">
          <ClockIcon className="h-3 w-3 mr-1" />
          ~{item.estimatedDeliveryMins} mins
        </div>
      </div>

      {/* Content */}
      <div className="p-4">
        {/* Name and Unit */}
        <div className="mb-2">
          <h3 className="font-medium text-gray-900 text-sm line-clamp-2">{item.name}</h3>
          <p className="text-xs text-gray-500 mt-1">{item.unit}</p>
        </div>

        {/* Price */}
        <div className="mb-2">
          <div className="flex items-baseline space-x-1">
            <span className="text-lg font-bold text-gray-900">₹{item.price.toFixed(2)}</span>
            {/* You can add MRP here if available */}
          </div>
        </div>

        {/* Stock Status */}
        <div className="mb-2">
          <span className={`text-xs font-medium ${stockStatus.color}`}>
            {stockStatus.text}
          </span>
        </div>

        {/* Distance */}
        <div className="mb-3 flex items-center text-xs text-gray-500">
          <MapPinIcon className="h-3 w-3 mr-1" />
          {item.distance.toFixed(1)} km away
        </div>

        {/* Add to Cart Button */}
        {quantity === 0 ? (
          <button
            onClick={handleAddToCart}
            disabled={!item.isAvailable || item.stockQuantity === 0}
            className={`w-full py-2 px-4 rounded-lg text-sm font-medium transition-colors ${
              item.isAvailable && item.stockQuantity > 0
                ? 'bg-emerald-600 text-white hover:bg-emerald-700'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {item.isAvailable && item.stockQuantity > 0 ? 'Add' : 'Unavailable'}
          </button>
        ) : (
          <div className="flex items-center justify-between">
            <button
              onClick={() => handleQuantityChange(-1)}
              className="p-2 rounded-full bg-gray-100 hover:bg-gray-200 transition-colors"
            >
              <MinusIcon className="h-4 w-4 text-gray-600" />
            </button>
            <span className="font-medium text-gray-900 px-3">{quantity}</span>
            <button
              onClick={() => handleQuantityChange(1)}
              disabled={quantity >= item.stockQuantity}
              className={`p-2 rounded-full transition-colors ${
                quantity >= item.stockQuantity
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-emerald-600 text-white hover:bg-emerald-700'
              }`}
            >
              <PlusIcon className="h-4 w-4" />
            </button>
          </div>
        )}

        {/* Store Name */}
        <div className="mt-2 text-xs text-gray-500 text-center">
          from {item.storeName}
        </div>
      </div>
    </div>
  );
}