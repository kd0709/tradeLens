export interface FavoriteToggleDto {
  productId: number
}

export interface FavoriteListDto {
    id: number;                   
    productId: number;
    title: string;             
    price: number;         
    imageUrl: string;           
    createTime: Date;   
}